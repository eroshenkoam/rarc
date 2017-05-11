pipeline {
    agent { label 'java' }
    tools { maven 'default' }
    stages {
        stage('Build') {
            steps {
                sh 'mvn -Dmaven.test.failure.ignore=true clean verify'
            }
        }
    }
}