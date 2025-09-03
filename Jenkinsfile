pipeline {
    agent none
    
    stages {
        stage('Build') {
            agent { label 'linux-jdk21' }

            steps {
                sh 'chmod +x gradlew'
                sh './gradlew bukkit:shadowJar'
                script {
                    def sanitizedBranch = env.BRANCH_NAME.replaceAll(/[^a-zA-Z0-9._-]/, '-')
                    def jars = findFiles(glob: 'bukkit/build/libs/TheBrewingProject*.jar')
                    
                    jars.each { jar ->
                        def newFileName = jar.name.replaceFirst(/\.jar$/, "-${sanitizedBranch}.jar")
                        sh "mv 'bukkit/build/libs/${jar.name}' 'bukkit/build/libs/${newFileName}'"
                    }
                }
            }

            post {        
                always {
                    archiveArtifacts artifacts: 'bukkit/build/libs/TheBrewingProject*.jar', fingerprint: true
                }
            }
        }
    }
}
