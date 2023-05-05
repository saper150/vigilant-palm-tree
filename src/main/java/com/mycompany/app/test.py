import requests
import json

from timeit import default_timer as timer
from functools import cmp_to_key


def solveTransaction(transactions):
    dict = {}

    for row in transactions:
        if not (row["debitAccount"] in dict):
            dict[row['debitAccount']] = {
                "account": row["debitAccount"], "debitCount": 0, "creditCount": 0, "balance": 0}

        if not (row["creditAccount"] in dict):
            dict[row["creditAccount"]] = {
                "account": row["creditAccount"], "debitCount": 0, "creditCount": 0, "balance": 0}

        debit = dict[row["debitAccount"]]
        credit = dict[row["creditAccount"]]

        debit["debitCount"] += 1
        debit["balance"] -= row["amount"]
        debit["balance"] = round(debit["balance"], 2)

        credit["creditCount"] += 1
        credit["balance"] += row["amount"]
        credit["balance"] = round(credit["balance"], 2)

    values = list(dict.values())
    # print(values)
    values.sort(key=lambda x: x["account"])
    return values


def compare(a, b):
    p = b["points"] - a["points"]
    if p != 0:
        return p

    return a["numberOfPlayers"] - b["numberOfPlayers"]


def soveGame(players):
    clans = list(players["clans"])
    clans.sort(key=cmp_to_key(compare))

    groupCount = players["groupCount"]
    groups = []
    addedCount = 0

    group = []

    avalibleSpace = groupCount

    while addedCount != len(clans):
        added = False
        for clan in clans:
            if (not ("deleted" in clan)) and clan["numberOfPlayers"] <= avalibleSpace:
                group.append(clan)
                clan["deleted"] = True
                added = True
                addedCount += 1
                avalibleSpace -= clan["numberOfPlayers"]

        if not added:
            groups.append(group)
            avalibleSpace = groupCount
            group = []

    if len(group) > 0:
        groups.append(group)

    for g in groups:
        for e in g:
            del e['deleted']

    return groups


requestTypeMap = {
    "PRIORITY": 1,
    "STANDARD": 3,
    "FAILURE_RESTART": 0,
    "SIGNAL_LOW": 2,
}


def atmCompare(a, b):
    return requestTypeMap[a["requestType"]] - requestTypeMap[b["requestType"]]


def solveAtm(requests):
    byRegion = {}

    for req in requests:
        if not (req["region"] in byRegion):
            byRegion[req["region"]] = []

        byRegion[req["region"]].append(req)

    for reg in byRegion.values():
        reg.sort(key=cmp_to_key(atmCompare))

    items = list(byRegion.items())
    items.sort(key=lambda x: x[0])

    result = []

    for (_, byReg) in items:
        added = set()
        for item in byReg:
            if not (item["atmId"] in added):
                del item["requestType"]
                result.append(item)
                added.add(item["atmId"])

    return result


def atm():
    url = "http://localhost:8080/atms/calculateOrder"

    data = [
        (json.load(open('./atmservice/example_1_request.json')),
         json.load(open('./atmservice/example_1_response.json'))),

        (json.load(open('./atmservice/example_2_request.json')),
         json.load(open('./atmservice/example_2_response.json'))),

        (json.load(open('./AtmGenerated.json')),
         json.loads(json.dumps(solveAtm(json.load(open('./AtmGenerated.json')))))),
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

        # p = open('./py.json', 'w')
        # j = open('./j.json', 'w')

        # p.write(json.dumps(expected, indent=4))
        # j.write(json.dumps(response.json(), indent=4))

        # print(response.json()[20])
        # print(expected[20])

        print("succ", response.json() == expected)


def game():

    url = "http://localhost:8080/onlinegame/calculate"

    data = [
        (json.load(open('./onlinegame/example_request.json')),
         json.load(open('./onlinegame/example_response.json'))),

        (json.load(open('./GameGenerated.json')),
         json.loads(json.dumps(soveGame(json.load(open('./GameGenerated.json')))))),
    ]
    # expected = [json.load(open('./transactionsExamples/example_1_response.json'))]

    for (data, expected) in data:

        start = timer()

        response = requests.get(url, json=data)

        end = timer()
        print("Time: ", (end - start) * 1000)
        # print("Status Code", response.status_code)
        # print("JSON Response ", response.json())
        # print("expected ", expected)

        print("succ", response.json() == expected)


def transaction():

    # print()

    url = "http://localhost:8080/transactions/report"

    data = [
        (json.load(open('./transactions/example_request.json')),
         json.load(open('./transactions/example_response.json'))),

        (json.load(open('./TransactionGenerated.json')),
         json.loads(json.dumps(solveTransaction(json.load(open('./TransactionGenerated.json')))))),
    ]

    for (data, expected) in data:

        start = timer()
        # ...

        response = requests.get(url, json=data)

        end = timer()
        print("Time: ", (end - start) * 1000)
        print("Status Code", response.status_code)
        # print("JSON Response ", response.json())
        # print("JSON Response ", response.json())

        print("succ", response.json() == expected)


print("testing game-------------------")
game()
print("testing transaction---------------------")
transaction()
print("testing atm-----------------------------")
atm()
