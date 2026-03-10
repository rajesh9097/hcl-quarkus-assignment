# Questions

Here are 2 questions related to the codebase. There's no right or wrong answer - we want to understand your reasoning.

## Question 1: API Specification Approaches

When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded everything directly. 

What are your thoughts on the pros and cons of each approach? Which would you choose and why?

**Answer:**
```txt
Direct coding allows complete control over the implememtation, easy to understnad
but it increases boilerplate code and we have to manually update API documentation
OpenAPI spec reduces boilerplate and ensures documentation is always up to date
need to be familiar with the tool and it can be less flexible for complex logic
```

---

## Question 2: Testing Strategy

Given the need to balance thorough testing with time and resource constraints, how would you prioritize tests for this project? 

Which types of tests (unit, integration, parameterized, etc.) would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```txt
will do unit test and then integration test, then parameterized test for edge cases
will use jaccoco ans sonar to monitor test coverage, will do proper postman testing for end-to-end api usecases
Automate test runs in CI/CD pipeline to check code coverage.

```
