# jenkins2_pipelines_pluralsight

Took a course on Pluralsight about Getting Started on Jenkins2. Experimented with the new pipeline as code. Improvised with my own learnings from other blogs and articles. Replaced old/legacy/deprecated approaches (that were described in the course) with new official approaches (as stated by updated plugin blogs/docs).

- Ran jenkins master server in official jenkins docker container.
- Attached my host linux ubuntu machine as a slave agent to the jenkins master server.
- Used official Mailhog container to test email notifications being sent by Jenkins for various user attentions.
- Used docker-compose to run our final deployment (a Javascript solitaire app) as a docker container.
