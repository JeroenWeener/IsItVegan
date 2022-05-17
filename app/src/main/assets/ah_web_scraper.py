from bs4 import BeautifulSoup
import requests
import re


def main():
    baseurl = 'https://www.ah.nl/producten/product/wi'
    all_ingredients = []

    for i in range(500000, 510001):
        url = baseurl + str(i)
        ingredients = parse_url(url)
        all_ingredients.extend(ingredients)

    all_ingredients = sorted(set(all_ingredients))

    # Write ingredients to file
    f = open('ingredient_list.txt', 'w', encoding='utf-8')
    for ingredient in all_ingredients:
        f.write(ingredient)
        f.write('\n')
    f.close()

    print('Finished extracting ingredients')


def parse_url(url):
    print('Scraping ' + url)

    req = requests.get(url)
    soup = BeautifulSoup(req.text, 'html.parser')

    ingredient_labels = soup.find_all('h2', text='Ingrediënten')
    if len(ingredient_labels) == 0:
        return []
    ingredients_div = ingredient_labels[0].find_parent('div')

    spans = ingredients_div.find_all('span')
    if len(spans) > 0:
        ingredient_text = spans[0].text
    else:
        ps = ingredients_div.find_all('p')
        if len(ps) > 0:
            ingredient_text = ps[0].text
        else:
            return []

    ingredient_text = re.sub(r'[0-9]+(,[0-9]+)?%', '', ingredient_text).split('.')[0]
    ingredients = ingredient_text \
        .replace('Ingrediënten: ', '') \
        .replace('(', ',') \
        .replace(')', '') \
        .replace('[', ',') \
        .replace(']', '') \
        .replace('*', '') \
        .replace('°', '') \
        .replace('ᵇ', '') \
        .replace(' ', ' ') \
        .lower() \
        .split(',')

    ingredients = [
        ingredient
            .replace(' ,', '')
            .replace('antioxidant: ', '')
            .replace('bevat: ', '')
            .replace('bevat:', '')
            .replace('bevat ', '')
            .replace('bevatten ', '')
            .replace('bevochtingsmiddel: ', '')
            .replace('conserveermiddel: ', '')
            .replace('conserveermiddelen: ', '')
            .replace('emulgator: ', '')
            .replace('geleermiddel: ', '')
            .replace('glansmiddel: ', '')
            .replace('glansmiddelen: ', '')
            .replace('ingrediënten: ', '')
            .replace('ingredienten: ', '')
            .replace('ingrediënten ', '')
            .replace('ingrediënten', '')
            .replace('kleurstof: ', '')
            .replace('kleurstoffen: ', '')
            .replace('onze belangrijkste bestanddelen zijn: ', '')
            .replace('rijsmiddel: ', '')
            .replace('rijsmiddelen: ', '')
            .replace('samenstelling: ', '')
            .replace('smaakversterker: ', '')
            .replace('smaakversterkers: ', '')
            .replace('stabilisator: ', '')
            .replace('stabilisatoren: ', '')
            .replace('verdikkingsmiddel: ', '')
            .replace('verdikkingsmiddelen: ', '')
            .replace('vitamines: ', '')
            .replace('voedingszuur: ', '')
            .replace('voedingszuren: ', '')
            .replace('zoetstof: ', '')
            .replace('zoetstoffen: ', '')
            .replace('zuurteregelaar: ', '')
            .strip() for ingredient in ingredients]
    ingredients = [ingredient for ingredient in ingredients if len(ingredient) > 0]
    return ingredients


if __name__ == '__main__':
    main()
