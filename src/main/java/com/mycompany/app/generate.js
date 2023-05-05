const fs = require("fs");

function requestType() {
  let t = [
    "PRIORITY",
    "SIGNAL_LOW",
    "FAILURE_RESTART",
    "STANDARD",
    "STANDARD",
    "STANDARD",
    "STANDARD",
    "STANDARD",
  ];

  return t[Math.floor(Math.random() * t.length)];
}

res = [];

for (let i = 0; i < 100000; i++) {
  res.push({
    region: Math.round(Math.random() * 200) + 1,
    requestType: requestType(),
    atmId: Math.round(Math.random() * 300) + 1,
  });
}

if (true) {
  fs.writeFileSync("./AtmGenerated.json", JSON.stringify(res));
}

{
  res = [];
  for (let i = 0; i < 20_000; i++) {
    res.push({
      numberOfPlayers: Math.floor(Math.random() * 200) + 1,
      points: Math.floor(Math.random() * 100_000) + 1,
    });
  }

  if (true) {
    fs.writeFileSync(
      "./GameGenerated.json",
      JSON.stringify(
        {
          groupCount: 1000,
          clans: res,
        },
        null,
        4
      )
    );
  }
}

function generateRandomNumericString() {
  let result = "";
  const chars = "0123456789";
  const length = 26;
  for (let i = 0; i < length; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return result;
}

{
  let accounts = [];
  for (let i = 0; i < 5000; i++) {
    accounts.push(generateRandomNumericString());
  }

  function generateRandomTransaction() {
    const debitAccount = accounts[Math.floor(Math.random() * accounts.length)];
    const creditAccount = accounts[Math.floor(Math.random() * accounts.length)];
    const amount = Math.round(Math.random() * 1000000) / 100;
    return { debitAccount, creditAccount, amount };
  }

  const res = [];
  for (let i = 0; i < 100_000; i++) {
    res.push(generateRandomTransaction());
  }
  if (false) {
    fs.writeFileSync("./TransactionGenerated.json", JSON.stringify(res));
  }
}
