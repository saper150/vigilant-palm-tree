import requests
import json

from timeit import default_timer as timer


def atm():
    url = "http://localhost:8080/atms/calculateOrder"

    data = [
        (json.load(open('./atmExamples/example_1_request.json')),
         json.load(open('./atmExamples/example_1_response.json'))),

        (json.load(open('./atmExamples/example_2_request.json')),
         json.load(open('./atmExamples/example_2_response.json'))),

        (json.load(open('./atmExamples/generated.json')),
         json.loads("[]")),
    ]
    # expected = [json.load(open('./transactionsExamples/example_1_response.json'))]

    for (data, expected) in data:

        start = timer()
        # ...

        response = requests.get(url, json=data)

        end = timer()
        print("Time: ", (end - start) * 1000)
        # print("Status Code", response.status_code)
        # print("JSON Response ", response.json())
        # print("expected ", expected)

        print("succ", response.json() == expected)


def game():
    url = "http://localhost:8080/onlinegame/calculate"

    data = [
        (json.load(open('./gameExamples/example_request.json')),
         json.load(open('./gameExamples/example_response.json'))),

        (json.load(open('./gameExamples/generated.json')),
         json.load(open('./gameExamples/example_response.json'))),
    ]
    # expected = [json.load(open('./transactionsExamples/example_1_response.json'))]

    for (data, expected) in data:

        start = timer()
        # ...

        response = requests.get(url, json=data)

        end = timer()
        print("Time: ", (end - start) * 1000)
        # print("Status Code", response.status_code)
        # print("JSON Response ", response.json())
        # print("expected ", expected)

        print("succ", response.json() == expected)


# game()


def transaction():
    url = "http://localhost:8080/transactions/report"

    data = [
        (json.load(open('./transactionsExample/example_request.json')),
         json.load(open('./transactionsExample/example_response.json'))),

        # (json.load(open('./transactionsExample/generated.json')),
        #  json.load(open('./transactionsExample/example_response.json'))),
    ]
    # expected = [json.load(open('./transactionsExamples/example_1_response.json'))]

    for (data, expected) in data:

        start = timer()
        # ...

        response = requests.get(url, json=data)

        end = timer()
        print("Time: ", (end - start) * 1000)
        print("Status Code", response.status_code)
        print("JSON Response ", response.json())
        # print("expected ", expected)

        print("succ", response.json() == expected)


transaction()
