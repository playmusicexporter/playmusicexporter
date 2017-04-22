pipeline {
    agent {
        docker 'gradle'
    }

    stages {
        stage('Build') {
            sh 'gradle build --info'
        }
    }
}