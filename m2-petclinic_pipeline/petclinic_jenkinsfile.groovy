#!/usr/bin/env groovy

node {
    try {
        notify('Started')

        stage('checkout'){
            git 'https://github.com/g0t4/jenkins2-course-spring-petclinic.git'
        }

        stage('compile test package'){
            sh 'mvn clean package'
        }

        notify("Success")
    }
    catch (err){
        echo "Caught: ${err}"
        currentBuild.result = "FAILURE"
        notify("Error ${err}")
    }

    stage('archival'){
        publishHTML([
            allowMissing: true,
            alwaysLinkToLastBuild: false,
            keepAll: true,
            reportDir: 'target/site/jacoco',
            reportFiles: 'index.html',
            reportName: 'Code Coverage'
        ])
        junit 'target/surefire-reports/TEST-*.xml'
        archiveArtifacts ([
            artifacts: "target/*.?ar",
            excludes: null
        ])
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
