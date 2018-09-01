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

        t.parseModes = dictionary.getParseModes();

        t.isNew = function () {
            return $routeParams.id == null;
        };

        t.refreshStatus = function() {
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
            api.sendMessage(t.message, !t.enableWebPagePreview, t.parseMode).then(function (sendingMessageStatus) {
                $location.path('/message/' + sendingMessageStatus.id);
            });
        };

        t.sendMessageToMe = function () {
            api.sendMessageToMe(t.message, !t.enableWebPagePreview, t.parseMode).then(function (sendingMessageStatus) {
                $location.path('/message/' + sendingMessageStatus.id);
            });
        };
        t.cancelSending = function () {
            api.cancelMessageSending($routeParams.id).then(function (data) {
                t.refreshStatus();
            });
        }


        t.refreshStatus();
    }
})();
