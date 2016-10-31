#!/usr/bin/env groovy

node {
    stage ('checkout install stash'){
        /*
        If the Jenkinsfile is kept in SCM,
        then jenkinsfile already knows which git repo should be checked out
        Hence, only a `checkout scm` step would suffice instead of the `git` step.
        */
        git url: 'https://github.com/g0t4/solitaire-systemjs-course',
            branch: 'jenkins2-course'
        // pull dependencies from npm
        sh 'npm install'
        // stash code & dependencies to expedite subsequent testing
        // and ensure same code & dependencies are used throughout the pipeline
        stash name: 'everything',
            includes: '**',
            excludes: 'test-results/**'
    }

    stage ('phantom test'){
        // test with PhantomJS for "fast" "generic" results
        try {
            sh "npm run test-single-run -- --browsers PhantomJS"
        }
        catch (err){
            echo "Could not pass PhantomJS test"
            echo "Caught: ${err}"
            email_notify("PhantomJS test failed!")
            // continue rest of the build
        }
    }

    stage ('archival'){
        // archive karma test results (karma is configured to export junit xml files)
        junit 'test-results/**/test-results.xml'
        // archive app artifacts
        archiveArtifacts artifacts: 'app/**', excludes: null
    }
}

stage ('parallel browser tests'){
    parallel chrome:{
        runBrowserTests('Chrome')
    }, firefox:{
        runBrowserTests('Firefox')
    },
    failFast: false
}

echo "Some echo messages outside any stage or node. Let's see where/how it is echoed."


stage ('deployment'){
    lock (resource: 'deployment', inversePrecedence: true ) {
        node {email_notify("Continue deployment?")}
        input "Proceed to deployment?"
        node ('Docker') {
            // write build number to index page so we can see this update
            // on windows use: bat "echo '<h1>${env.BUILD_DISPLAY_NAME}</h1>' >> app/index.html"
            sh "echo '<h1>${env.BUILD_DISPLAY_NAME}</h1>' >> app/index.html"
            // deploy to a docker container mapped to port 3000
            // on windows use: bat 'docker-compose up -d --build'
            sh 'docker-compose up -d --build'
            email_notify("Successfully deployed!")
        }
    }
}

def runBrowserTests(browserName){
    // agent nodes must first be configured to hava labels based on the browsers that they support
    node (browserName) {
        echo "Will run browser tests for '${browserName}' browser"
        sh 'pwd'
        sh 'ls'
        sh 'rm -rf *'
        sh 'ls'
        unstash 'everything'
        sh "npm run test-single-run -- --browsers ${browserName}"
        junit 'test-results/**/test-results.xml'
        sh 'ls'
    }
}

def email_notify(status){
    emailext (
      to: "wesmdemos@gmail.com",
      subject: "${status}: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
      body: """<p>${status}: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':</p>
        <p>Check console output at <a href='${env.BUILD_URL}'>${env.JOB_NAME} [${env.BUILD_NUMBER}]</a></p>""",
    )
}
