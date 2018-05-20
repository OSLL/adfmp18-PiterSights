#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import lxml
import lxml.html.clean
import requests
import wikipedia as wp
from sys import stderr
from transliterate import translit


def get_html_from_text(raw_html):
    # clean_args = {
    #     "javascript": True,  # strip javascript
    #     "page_structure": False,  # leave page structure alone
    #     "style": True  # remove CSS styling
    # }
    # clean_html = lxml.html.clean.Cleaner(**clean_args).clean_html(raw_html)
    html = lxml.html.fromstring(raw_html)
    return html


def get_element_by_selector(b, selector):
    select_result = list(b.cssselect(selector))
    if len(select_result) == 0:
        return None
    return select_result[0]


def get_info_from_block(b):
    d = dict()
    p = get_element_by_selector(b, 'p[itemprop="description"]')
    d['short'] = '\n'.join(t for t in p.itertext()) if p is not None else None
    # short = '\n'.join(t for t in p.itertext())

    label_select = get_element_by_selector(b, 'h3 > a')
    d['label'] = label_select.text if label_select is not None else None

    lat_select = get_element_by_selector(b, 'meta[itemprop="latitude"]')
    d['lat'] = lat_select.attrib['content'] if lat_select is not None else None

    long_select = get_element_by_selector(b, 'meta[itemprop="longitude"]')
    d['long'] = long_select.attrib['content'] if long_select is not None else None

    return d
    # print(d)


def get_infos():
    response = requests.get('https://autotravel.ru/excite.php/1055/1')
    raw_html = response.text
    # BLOCK_SELECTOR = 'div[itemtype="http://schema.org/Place"]'
    BLOCK_SELECTOR = 'div[class="col-md-12 col-xs-12"] > div'

    html = get_html_from_text(raw_html)
    blocks = html.cssselect(BLOCK_SELECTOR)
    infos = list(filter(lambda d: d['long'] is not None and d['lat'] is not None, map(get_info_from_block, blocks)))
    return infos


def check_label(d, key):
    return type(d[key]) == str and d[key].find('\n') == -1


def search(query, lang):
    wp.set_lang(lang)
    wp_search = wp.search(query)
    if len(wp_search) == 0:
        return None
    return wp_search[0]


def get_page(d):
    label = d['label']
    ru_label = d['label_ru']

    exception = wp.exceptions.WikipediaException

    def try_different(suffix=''):
        try:
            p = wp.page(label + suffix)
        except exception:
            try:
                p = wp.page(ru_label + suffix)
            except exception:
                p = None
        return p
    p = try_different()
    if p is None:
        p = try_different(' (Санкт-Петербург)')
    return p


class ExtractorError(RuntimeError):
    def __init__(self, message):
        super(ExtractorError, self).__init__(message)
        self.message = message

OUTPUT_DIRECTORY = "/home/kravtsun/Dropbox/sights"
if __name__ == '__main__':
    infos = get_infos()
    bad_records = []
    for i, d in enumerate(infos):
        try:
            label = d['label']
            # en_search = search(label, 'en')
            # if en_search is None:
            en_search = translit(label, reversed=True)
            d['label_en'] = en_search
            assert check_label(d, 'label_en')

            ru_search = search(label, 'ru')
            if ru_search is None:
                raise ExtractorError('ru_search')
            d['label_ru'] = ru_search
            assert check_label(d, 'label_ru')

            p = get_page(d)
            if p is None:
                raise ExtractorError('get_page')
            if d['short'] is None:
                d['short'] = p.summary
            try:
                d['lat'] = float(p.coordinates[0])
                d['long'] = float(p.coordinates[1])
            except KeyError:
                pass
            d['url'] = p.url
            d['name'] = ''.join(filter(lambda c: c.isalnum() or c == '_', d['label_en'].replace(' ', '_')))
            if d['name'].startswith('List'):
                print(d)
            f = open('sights/' + d['name'] + '.sight', 'w')
            f.write('\n'.join([
                d['label'],
                d['label_ru'],
                d['label_en'],
                str(d['lat']) + ' ' + str(d['long']),
                d['url'],
                '===',
                d['short'],
            ]))
            f.close()
        except ExtractorError as e:
            print(i)
            stderr.write(e.message + '\n')
            bad_records.append(d)
    f = open('bad_records.txt', 'w')
    f.write('\n'.join([str(record) for record in bad_records]))
    f.close()


