
void call(String buildTool = 'maven') {
    pipeline {
        agent any
        environment {
            CURRENT_STAGE = ''
        }
        stages {
            stage('pipeline') {
                steps {
                    script {

                        // Validar formato de nombre de rama release
                        getReleaseVersion()

                        if (buildTool == 'maven') {
                            maven.call(getPipelineType())
                        } else {
                            gradle.call(getPipelineType())
                        }
                    }
                }
            }
        }
        post {
            success {
                //slackSend(color: '#00FF00', message: '[gamboa][' + env.JOB_NAME + '][' + buildTool + '] Ejecución Exitosa.')
                slackSend color: 'good', message: "[Secc2-Grp4][Pipeline: ${env.buildTool}][Rama: ${env.BRANCH_NAME}][Stage:${CURRENT_STAGE}] Ejecucion exitosa."
            }
            failure {
                //slackSend(color: '#FF0000', message: '[gamboa][' + env.JOB_NAME + '][' + buildTool + '] Ejecución Fallida en Stage [' + env.CURRENT_STAGE + '].')
                slackSend color: 'danger', message: "[Secc2-Grp4][Pipeline: ${env.buildTool}][Rama: ${env.BRANCH_NAME}] Ejecucion fallida en stage [${CURRENT_STAGE}]."
            }
        }
    }
}

String getBuildTool() {
    return '';
}

// Obtención de versión en formato "v#-#-#" de rama release
// si no viene en formato definido lanza error
String getReleaseVersion(){
    if (env.GIT_BRANCH.contains('release')){
        version = ( env.GIT_BRANCH =~ /release-(v\d+\-\d+\-\d+)/)
        if(version.find()){
            return version.group(1)
        }
        else{
            throw new Exception('Formato rama release inválido: ' + env.GIT_BRANCH )
        }
    }
}

String getPipelineType() {
    if (env.GIT_BRANCH.contains('feature-'))
        return 'CI-Feature'
    else if (env.GIT_BRANCH.equals('develop'))
        return 'CI-Develop'
    else if (env.GIT_BRANCH.contains('release-'))
        return 'CD'
}

return this
