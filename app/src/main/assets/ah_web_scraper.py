from bs4 import BeautifulSoup
import requests

url = "https://www.ah.nl/producten/product/wi236486"
req = requests.get(url)
soup = BeautifulSoup(req.text, "html.parser")

ingredients_label = soup.find_all("h2", text="Ingrediënten")[0]
ingredients_div = ingredients_label.find_parent("div")
ingredient_text = ingredients_div.find_all("span")[0].text
ingredients = ingredient_text\
    .replace("Ingrediënten: ", "")\
    .replace("(", ', ')\
    .replace(')', '')\
    .replace('%', '')\
    .replace('0', '')\
    .replace('1', '')\
    .replace('2', '')\
    .replace('3', '')\
    .replace('4', '')\
    .replace('5', '')\
    .replace('6', '')\
    .replace('7', '')\
    .replace('8', '')\
    .replace('9', '')\
    .split(', ')

for ingredient in ingredients:
    print(ingredient)
