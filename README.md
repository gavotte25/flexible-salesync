# flexible-salesync customized for tenant-b
Customized service:
Record: Integrate with Slack, allow service to post message in a slack channel when a record phase is updated. Logic of message posting can be configured in json format and inputted by admin on web app following the existing UI/UX.

## Prerequisite
Base system on main branch of flexible-salesync has already been deployed and tenant-b account is already created.
Generate Slack Webhook url according to this [tutorial](https://docs.slack.dev/messaging/sending-messages-using-incoming-webhooks/#create_a_webhook).

## Clone project
```
git clone -b main-tenant-b https://github.com/gavotte25/flexible-salesync.git flexible-salesync-tenant-b
cd flexible-salesync-tenant-b
git submodule update --init --recursive --force --remote
```
## Update environment
Replace SLACK_WEBHOOK_URL value with your generated Slack webhook url

## Build image and deploy
```
docker build -t record-tenant-b record/
minikube image load record-tenant-b:latest
kubectl apply -f yaml-kubectl/.
```

## Configure message posting logic
Login as tenant-b admin, create new object with name "Setting' with 2 properties: "Name" & "Json Config".
Create new record name "Slack Setting" with json config field follows this format
```
{
    "<OBJECT_NAME_1>:<STAGE_NAME_1>": "<MESSAGE_CONTENT_1>",
    "<OBJECT_NAME_2>:<STAGE_NAME_2>": "<MESSAGE_CONTENT_2>"
    ...
}
```
Message content can be dynamic, with 2 built-in placeholders: <USER_NAME_PLACEHOLDER> & <RECORD_NAME_PLACEHOLDER>.
Example
```
{ 
    "Opportunity:Closed Won": "Congrat <USER_NAME_PLACEHOLDER> for winning opportunity <RECORD_NAME_PLACEHOLDER>!" 
}
```
With the above example setting, everytime an opportunity record haviing stage is updated to Closed Won, a new messsage will be sent to Slack with the defined content.