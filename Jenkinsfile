pipeline {
    agent {
        docker 'gradle'
    }

    stages {
        stage('Build') {
            steps {
                echo 'Building..'
                sh './gradlew build --info'
            }
        }
    }
}