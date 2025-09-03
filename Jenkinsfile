pipeline {
    agent any
    
    stages {
        stage('Build') {
            agent {
                docker { image 'jenkins/agent:jdk21' }
            }

            steps {
                sh 'chmod +x gradlew'
                sh './gradlew bukkit:shadowJar'
            }
        }
    }
}
