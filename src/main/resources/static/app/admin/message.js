(function () {
    'use strict';
    angular.module('message', ['ngTouch'])
            .controller('MessageController', MessageController);
    function MessageController($log,
                               $routeParams,
                               $location,
                               api,
                               dictionary) {
        var t = this;
        t.isSendingToAll = true;
        t.sendCount = 0;

        t.parseModes = dictionary.getParseModes();

        t.isActive = false;

        t.isNew = function () {
            return $routeParams.id == null;
        };

        t.refreshStatus = function() {
            t.isCancelAllowed = false;
            t.isSendingAllowed = true;
            t.message = "";
            t.parseMode = null;
            t.enableWebPagePreview = false;
            if (!t.isNew()) {
                api.getMessageStatus($routeParams.id).then(function (sendingMessageStatus) {
                    t.status = sendingMessageStatus;
                    if (sendingMessageStatus.sendMessageData != null) {
                        t.message = sendingMessageStatus.sendMessageData.text;
                        t.parseMode = sendingMessageStatus.sendMessageData.parseMode;
                        t.enableWebPagePreview = !sendingMessageStatus.sendMessageData.disableWebPagePreview;
                    } else {
                        t.message = sendingMessageStatus.forwardMessageData.text;
                        t.parseMode = null;
                        t.enableWebPagePreview = false;
                    }
                    t.statusDescription = '';
                    Object.keys(t.status.statuses).forEach(function(key,index) {
                        t.statusDescription += '\n\t'+ t.getStatusDescription(key) + ':' + t.status.statuses[key];
                    });
                    t.isCancelAllowed = t.status.statuses['READY'] > 0;
                    t.isSendingAllowed = t.status.statuses['CREATED'] > 0 || t.status.statuses['ERROR'] > 0 ||
                        t.status.statuses['CANCELLED'] > 0 || t.status.statuses['FINISHED'] > 0;
                });
            }
        };

       t.getStatusDescription = function (status) {
            switch (status) {
                case 'CREATED':
                    return 'Создано';
                case 'READY':
                    return 'Инициирована отправка';
                case 'STARTED':
                    return 'Отправляется';
                case 'ERROR':
                    return 'Ошибка отправки';
                case 'CANCELLED':
                    return 'Отправка отменена';
                case 'FINISHED':
                    return 'Отправлено';
                default :
                    return '';
            }
        };

        t.sendMessage = function () {
            if (t.isNew()) {
                api.sendMessage(t.message, !t.enableWebPagePreview, t.parseMode).then(function (sendingMessageStatus) {
                    $location.path('/message/' + sendingMessageStatus.id);
                });
            }
        };

        t.startSending = function() {
                            var count = t.sendCount;
                            if (t.isSendingToAll) {
                                count = -1;
                            }
                            api.startSending($routeParams.id, count).then(function(sendingMessageStatus) {
                                t.refreshStatus();
                            });
        }

        t.sendMessageToMe = function () {
            if (t.isNew()) {
                api.sendMessageToMe(t.message, !t.enableWebPagePreview, t.parseMode).then(function (sendingMessageStatus) {
                    $location.path('/message/' + sendingMessageStatus.id);
                });
            }
        };

        t.cancelSending = function () {
            api.cancelMessageSending($routeParams.id).then(function (data) {
                t.refreshStatus();
            });
        }


        t.refreshStatus();
    }
})();
