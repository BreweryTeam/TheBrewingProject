pipeline {
    agent none

    tools {
        jdk "jdk-21.0.8"
    }

    stages {
        stage('Build') {
            agent any

            steps {
                sh 'chmod +x gradlew'
                sh './gradlew bukkit:shadowJar'
            }
        }
    }
}
