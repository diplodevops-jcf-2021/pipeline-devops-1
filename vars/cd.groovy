void call() {

    String gitDiff         = "gitDiff"
    String nexusDownload   = 'nexusDownload'
    String run             = "run"
    String test            = "test"
    String gitMergeMaster  = 'gitMergeMaster'
    String gitMergeDevelop = 'gitMergeDevelop'
    String gitTagMaster    = 'gitTagMaster'
 
    String[] stages = [
        gitDiff,
        nexusDownload,
        run,
        test,
        gitMergeMaster,
        gitMergeDevelop,
        gitTagMaster
    ]

    String[] currentStages = []
    currentStages = stages

    if (stages.findAll { e -> currentStages.contains( e ) }.size() == 0) {
        throw new Exception('Al menos una stage es inválida. Stages válidas: ' + stages.join(', ') + '. Recibe: ' + currentStages.join(', '))
    }

    // gitDiff
    if (currentStages.contains(gitDiff)) {
        stage(gitDiff) {
            env.CURRENT_STAGE = gitDiff
            figlet env.CURRENT_STAGE
            sh "git config --add remote.origin.fetch +refs/heads/main:refs/remotes/origin/main"
            sh "git fetch --no-tags"
            sh "git diff origin/main origin/${GIT_LOCAL_BRANCH}"
        }
    }

    // nexusDownload
    if (currentStages.contains(nexusDownload)) {
        stage(nexusDownload) {
            env.CURRENT_STAGE = nexusDownload
            figlet env.CURRENT_STAGE
            withCredentials([usernameColonPassword(credentialsId: env.NEXUS_CREDENTIALS_ID, variable: 'NEXUS_CREDENTIALS')]) {
                String repoUrl = "${env.NEXUS_URL}/repository/${env.NEXUS_REPOSITORY_ID}/${env.ARTIFACT_GROUP_ID}/${env.ARTIFACT_ID}/${env.ARTIFACT_VERSION}/${env.ARTIFACT_ID}-${env.ARTIFACT_VERSION}.jar";
                println repoUrl
                sh 'curl -u ${NEXUS_CREDENTIALS} "' + repoUrl + '" -O'
            }
        }
    }

    // run
    if (currentStages.contains(run)) {
        stage(run) {
            env.CURRENT_STAGE = run
            figlet env.CURRENT_STAGE
            sh "java -jar ${env.ARTIFACT_ID}-${env.ARTIFACT_VERSION}.jar &"
            sleep 20
        }
    }
    
    // test
    if (currentStages.contains(test)) {
        stage(test) {
            env.CURRENT_STAGE = test
            figlet env.CURRENT_STAGE
            sh 'curl -X GET http://localhost:8081/rest/mscovid/test?msg=testing'
        }
    }

    // gitMergeMaster
    if (currentStages.contains(gitMergeMaster)) {
        stage(gitMergeMaster) {
            env.CURRENT_STAGE = gitMergeMaster
            figlet env.CURRENT_STAGE
            def git = new helpers.Git()
            git.merge("${env.GIT_LOCAL_BRANCH}",'main')
            println "${env.STAGE_NAME} realizado con exito"
        }
    }
    
    // gitMergeDevelop
    if (currentStages.contains(gitMergeDevelop)) {
        stage(gitMergeDevelop) {
            env.CURRENT_STAGE = gitMergeDevelop
            figlet env.CURRENT_STAGE
            def git = new helpers.Git()
            git.merge("${env.GIT_LOCAL_BRANCH}", 'develop')
            println "${env.STAGE_NAME} realizado con exito"
        }
    }
    
    // gitTagMaster
    if (currentStages.contains(gitTagMaster)) {
        stage(gitTagMaster) {
            env.CURRENT_STAGE = gitTagMaster
            figlet env.CURRENT_STAGE
            def git = new helpers.Git()
            git.tag("${env.GIT_LOCAL_BRANCH}")
            println "${env.STAGE_NAME} realizado con exito"
        }
    }
}

return this