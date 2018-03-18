'use strict'

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.sendNotification = functions.database
    .ref('/notifications/{user_id}/{notification_id}')
    .onWrite(event => {

        const user_id = event.params.user_id;
        const notification = event.params.notification;

        console.log(`send to: ${user_id}`);

        if (!event.data.val()) {
            return console.log(`notification does not exist`);
        }

        const deviceToken = admin.database.ref(`/users/${user_id}/token`).once(`value`);
        return deviceToken.then(result => {

            const tokenId = result.val();

            const payload = {
                notification: {
                    title: "Spond Message",
                    body: "You received a message",
                    icon: "default"
                }
            };

            return admin.messaging().sendToDevice(tokenId, payload).then(response => {

                return console.log(`notification sent to ${user_id}`);

            });
        });
    });