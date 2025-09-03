pipeline {
    agent none
    
    stages {
        stage('Build') {
            agent { label 'linux-jdk21' }

            steps {
                sh 'chmod +x gradlew'
                sh './gradlew bukkit:shadowJar'
            }
        }
    }
}
