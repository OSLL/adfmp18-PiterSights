#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Script for getting detailed on sights coming from stdio or via function
"""
from sys import stdin, stdout, argv
import wikipedia as wp
wp.set_lang('ru')
ENCODING = 'utf8'


class Description:
    def __init__(self, short, long=None, coordinates=None):
        self.short = short
        self.long = long
        self.coordinates = coordinates

    def get_long(self) -> str:
        return self.short if self.long is None else self.long

    def get_short(self):
        return self.short

    def get_coordinates(self):
        return self.coordinates


def long_description_from_page(page: wp.WikipediaPage):
    return page.content


def get_description(sight_name : str) -> Description:
    results = wp.search(sight_name + ' Санкт-Петербург')
    if len(results) == 0:
        raise ValueError("Bad sight_name: " + sight_name)
    name = results[0]
    page = wp.page(name)
    short = page.summary
    long = long_description_from_page(page)
    coordinates = page.coordinates
    return Description(short, long, coordinates)


def debug(s):
    stdout.write(s)


if __name__ == '__main__':
    if len(argv) > 1:
        u = ' '.join(argv[1:])
    else:
        u = stdin.readline().strip()
        # u = l.decode('utf8')
    d = get_description(u)
    template = "Name: {}\n------======------\nShort: {}\n------======------\nLong: {}\n------======------\nCoordinates: {}"
    strs = (u, d.get_short(), d.get_long(), str(d.get_coordinates()))
    stdout.write(template.format(*strs))

