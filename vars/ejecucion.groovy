
void call(String buildTool = 'maven') {
    pipeline {
        agent any
        stages {
            stage('pipeline') {
                steps {
                    script {
                        String pipelineType = getPipelineType()
                        env.CURRENT_STAGE = ''
                        env.PIPELINE = getPipeline(pipelineType)
                        try {
                            if (buildTool == 'maven') {
                                maven.call(pipelineType)
                            } else {
                                gradle.call(pipelineType)
                            }
                            slackSend color: 'good', message: "[Grupo4][Pipeline ${env.PIPELINE}][Rama: ${env.GIT_LOCAL_BRANCH}][Stage:${env.CURRENT_STAGE}][Resultado: Ok]"
                        } catch(Exception e) {
                            slackSend color: 'danger', message: "[Grupo4][Pipeline ${env.PIPELINE}][Rama: ${env.GIT_LOCAL_BRANCH}][Stage:${env.CURRENT_STAGE}][Resultado: No Ok]"
                        }
                    }
                }
            }
        }
    }
}

String getPipeline(String pipelineType) {
    String pipeline = ''
    if (pipelineType.contains('CI')) {
        pipeline = 'IC'
    } else if (pipelineType.contains('CD')) {
        pipeline = 'Release'
    }

    return pipeline
}

// Si no cumple con el patrón de ninguna de las 3 opciones, lanzará error
String getPipelineType(){
    if (env.GIT_LOCAL_BRANCH ==~ /^release-v(\d+)-(\d+)-(\d+)$/) {
        return 'CD'
    } else if (env.GIT_LOCAL_BRANCH ==~ /^develop$/) {
        return 'CI-Develop'
    } else if (env.GIT_LOCAL_BRANCH ==~ /^feature-(.+)$/) {
        return 'CI-Feature'
    } else {
        throw new Exception('Formato rama inválido: ' + env.GIT_LOCAL_BRANCH )
    }
}

return this
