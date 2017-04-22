pipeline {
    agent {
        docker 'gradle'
    }

    stages {
        stage('Build') {
            echo 'Building..'
            sh 'gradle build --info'
        }
    }
}