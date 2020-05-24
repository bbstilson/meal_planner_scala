# Meal Planner

My partner and I are both picky about what we eat, and also *hate* choosing what to eat.

To solve the former problem, we've built up a few dozen recipes over the years that meet several criteria specific to our diets, desires and constraints. We keep these in Trello.

To solve the latter problem, I used ༼つ ◕_◕ ༽つ ***T E C H N O L O G Y***.

The "algorithm" is laid out in [`Main.scala`](./meal_planner/src/org/bbstilson/Main.scala).

## Technology

[Lambda](https://aws.amazon.com/lambda/) is used to run the code.

[Cloudwatch Events](https://docs.aws.amazon.com/AmazonCloudWatch/latest/events/WhatIsCloudWatchEvents.html) are used to trigger the code once per week.

[S3](https://aws.amazon.com/s3/) is used to store a CSV that keeps track of how many times a meal has been suggested.

[SES](https://aws.amazon.com/ses/) is used to email my partner and I with the meals for the week.

## Running locally

The build tool is [mill](http://www.lihaoyi.com/mill/).

```bash
mill meal_planner.run
```

## Testing

```bash
mill meal_planner.test
```
