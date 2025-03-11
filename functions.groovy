// l. 341
import groovy.json.JsonOutput

def readCommitAuthor() {
    sh '''#!/bin/bash
        git rev-parse HEAD | tr '\n' ' ' > gitCommit
        git show --format="%aN <%aE>" ${gitCommit} | head -1 | tr '\n' ' ' > gitCommitAuthor
    '''
    return readFile('gitCommitAuthor')
}

def durationTime(m1, m2) {
    int timecase = m2 - m1

    int seconds = (int) (timecase / 1000)
    int minutes = (int) (timecase / (60*1000))
    int hours = (int) (timecase / (1000*60*60))

    return hours.mod(24) + "h " + minutes.mod(60) + "m " + seconds.mod(60) + "s"
}

def findPodsFromName(String namespace, String name) {
    podsAndImagesRaw = sh(
        script: """
            kubectl get pods -n ${namespace} --selector=app=${name} -o jsonpath='{range .items[*]}{.metadata.name}###'
        """,
        returnStdout: true
    ).trim()
    wantedPods = podsAndImagesRaw.split('###')

    return wantedPods
}

// l. 342 - 343
def notifySlack(text, channel, attachments) {
    // https://api.slack.com/apps/A08H48EP0EP/incoming-webhooks?success=1
    // Get your own slack webhook url and token
    // def slackURL = 'https://hooks.slack.com/services/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'
    // def slackUrl = 'https://hooks.slack.com/services/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'
    // def slackUrl = 'https://hooks.slack.com/services/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'
    def slackUrl = 'https://hooks.slack.com/services/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'
    def jenkinsIcon = 'https://a.slack-edge.com/205a/img/services/jenkins-ci_72.png'

    def payload = JsonOutput.toJson([
        text: text,
        channel: channel,
        username: "jenkins",
        icon_url: jenkinsIcon,
        attachments: attachments
    ])

    sh "curl -s -X POST ${slackURL} -H 'Cache-Control: no-cache' -H 'Content-Type: application/json;charset=UTF-8' -d '${payload}'"
}

return this