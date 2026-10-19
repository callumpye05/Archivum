## Project journal 



This journal aims to highlight what I'm doing more closely with updates based on sessions, I also plan on using it to track my current progress. 
Unfortunately It's coming roughly 7 weeks after starting the project, however for what's to follow, it is necessary. 


## 10/19/2026 

During today's session, I noticed that my Docker containers were no longer starting automatically, requiring me to start them manually.
I also implemented two integration tests for user authentication. Unlike my previous tests, which relied on users manually created in the database, these tests create their own entities during setup.
However, I encountered an issue with test repeatability and database isolation.The tests were inserting records directly into the development database, meaning follow-up executions failed due to duplicate entries.
As a temporary solution I ended up  introducing Spring's @Transactional annotation, allowing test-managed transactions to be rolled back automatically after execution.
While researching this approach, I came across a 2020 Medium article discussing the potential pitfalls of using @Transactional in integration tests.
One concern that they speak of is that transactional tests may produce false positives, where tests pass despite problems that would occur under normal application conditions. For example, the transaction provided by the test framework may conceal missing transactional boundaries in application code.
Furthermore, methods using Propagation .REQUIRED may join the transaction already established by the test framework. Although this is expected Spring behavior, it can result in tests executing under different transaction boundaries from those encountered in production, which is a bad practice.
Consequently, I have added the creation of an isolated integration-testing environment to the TODO list. I intend to learn about Testcontainers.
For the time being, the existing tests will serve as a temporary solution. However, I do not consider the integration-testing infrastructure complete ( I still have some more to do, but I will actively focus on the new isolated environment).
