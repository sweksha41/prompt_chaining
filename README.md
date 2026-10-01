In This project I have implemented prompt chaining where I take response of one prompt and pass 
it to other prompt to solve the purpose of my application.
- In one prompt, I am extracting job description and then passing it to another prompt along with the skill extracted from a résumé in a separate prompt.  
The final result is the score of the résumé based on the job description provided.


prerequisites:
- Java 25
- GROQ apikey generated from https://console.groq.com/ and added to environment variable

Steps to run:
- Import the pom.xml
- mvn clean install
- start the PromptChainApplication
- Hit prom postman/Bruno a post call http://localhost:8080/api/agent/match with
POST /api/groq/match
  Content-Type: application/json

{
"jobDescription": "Backend Python Developer. Requirements: Python, FastAPI or Django, PostgreSQL, Docker, AWS, REST APIs, 2+ years of experience.",
"resume": "3 years as a Software Developer. Skills: Python, FastAPI, MySQL, Docker, REST APIs, Git."
}


Groq Models:
- You can find the groq model accessible to you on your groq logged-in portal
<img width="295" height="500" alt="image" src="https://github.com/user-attachments/assets/8fb92839-b0a5-42d2-91a0-896df718fe9c" />


This is the way to call it 

POST /api/groq/match
Content-Type: application/json

{
"jobDescription": "Backend Python Developer. Requirements: Python, FastAPI or Django, PostgreSQL, Docker, AWS, REST APIs, 2+ years of experience.",
"resume": "3 years as a Software Developer. Skills: Python, FastAPI, MySQL, Docker, REST APIs, Git."
}

![img_2.png](img_2.png)


For streaming, we just pass "stream" to true in the body and the LLM understands that we want to stream the result instead of waiting for complete response to be ready.

Map<String, Object> requestBody = Map.of(
"model", MODEL,
"messages", List.of(
Map.of("role", "user", "content", prompt)
),
"stream", true
);


For testing streaming end point http://localhost:8080/api/agent/ask/stream
**It doesn't show nicely on postman as shown below**

![img_1.png](img_1.png)


**so use the frontend page created below to test it**
http://localhost:8080/stream-test.html

![img.png](img.png)