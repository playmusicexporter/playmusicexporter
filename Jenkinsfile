pipeline {
    agent {
        docker 'gradle'
    }

    stages {
        stage('Build') {
            steps {
                echo 'Building..'
                sh 'gradle build --info'
            }
        }
    }
}