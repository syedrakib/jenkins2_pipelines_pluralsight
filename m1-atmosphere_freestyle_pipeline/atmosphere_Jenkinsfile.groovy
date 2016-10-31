#!/usr/bin/env groovy

node ('master'){

    echo "${env.BUILD_URL}"

    try {

        stage('checkout') {
            git 'https://github.com/g0t4/jenkins2-course-spring-boot.git'
        }

        def project_path = "spring-boot-samples/spring-boot-sample-atmosphere"

        dir(project_path) {
            stage('compile test package') {
                sh 'mvn clean package'
            }

            stage('archival') {
                archiveArtifacts ([
                    artifacts: "target/*.jar",
                    excludes: null
                ])
            }
        }

        notify("Success")

    }

    catch (err){
        echo "Caught: ${err}"
        currentBuild.result = "FAILURE"
        notify("Error ${err}")
    }
}

def notify(status){
    emailext (
      to: "syed.rakib.al.hasan@gmail.com",
      subject: "${status}: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
      body: """<p>${status}: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':</p>
        <p>Check console output at <a href='${env.BUILD_URL}console'>${env.JOB_NAME} [${env.BUILD_NUMBER}]</a></p>""",
    )
}
