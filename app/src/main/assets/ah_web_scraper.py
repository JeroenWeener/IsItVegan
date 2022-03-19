from bs4 import BeautifulSoup
import requests
import re

def main():
    baseurl = 'https://www.ah.nl/producten/product/wi'
    all_ingredients = []

    for i in range(507377, 507387):
        url = baseurl + str(i)
        ingredients = parseUrl(url)
        all_ingredients.extend(ingredients)

    print(all_ingredients)


def parseUrl(url):
    req = requests.get(url)
    soup = BeautifulSoup(req.text, 'html.parser')

    ingredient_labels = soup.find_all('h2', text='Ingrediënten')
    if len(ingredient_labels) == 0:
        return []
    ingredients_div = ingredient_labels[0].find_parent('div')
    ingredient_text = ingredients_div.find_all('span')[0].text
    ingredient_text = re.sub(r'[0-9]+(,[0-9]+)?%', '', ingredient_text).split('.')[0]
    ingredients = ingredient_text\
        .replace('Ingrediënten: ', '')\
        .replace('(', ',')\
        .replace(')', '')\
        .replace('[', ',')\
        .replace(']', '')\
        .replace('*', '')\
        .replace('°', '')\
        .replace('ᵇ', '')\
        .lower()\
        .split(',')

    ingredients = [ingredient.replace(' ,', '').replace('bevatten ', '').replace('conserveermiddel: ', '').replace('voedingszuur ', '').replace('bevat: ', '').replace('van ', '').strip() for ingredient in ingredients]
    ingredients = [ingredient for ingredient in ingredients if len(ingredient) > 0]
    return ingredients


if __name__ == "__main__":
    main()
