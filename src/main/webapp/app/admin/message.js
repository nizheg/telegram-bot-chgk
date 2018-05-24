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
        t.status = {};
        t.statusDescription = "";
        t.enableWebPagePreview = false;
        t.parseMode = null;
        t.parseModes = dictionary.getParseModes();
        api.getMessageStatus().then(function (data) {
                    t.status = data;
                    t.statusDescription = t.printStatus(data);
                }
        );

        t.printStatus = function (status) {
            switch (status.status) {
                case 'NOT_STARTED':
                    return 'Запущенного задания отправки нет';
                case 'FORWARD_INITIATED':
                    return 'Forward-сообщение инициировано:\n' + status.message;
                case 'REJECTED':
                    return 'Ошибка. ' + status.message;
                case 'IN_PROCESS':
                    return 'Отправка в процессе.\nВыполнено: ' + status.finished + ' из ' + status.totalCount +
                            '\nОтправляемое сообщение: \n' + status.message;
                case 'FINISHED':
                    return 'Отправка завершена. Отправлено ' + status.finished + ' из ' + status.totalCount +
                            '\nОтправленное сообщение: \n' + status.message;
                case 'CANCELLED':
                    return 'Отправка отменена. Отправлено ' + status.finished + ' из ' + status.totalCount +
                            '\nОтправляемое сообщение: \n' + status.message;
                default :
                    return '';
            }

        };

        t.isInProcess = function () {
            return 'IN_PROCESS' == t.status.status;
        }

        t.isInitiated = function() {
            return 'FORWARD_INITIATED' == t.status.status;
        }

        t.sendMessage = function () {
            api.sendMessage(t.message, !t.enableWebPagePreview, t.parseMode).then(function (data) {
                t.status = data;
                t.statusDescription = t.printStatus(data);
            });
        };
        t.cancelSending = function () {
            api.cancelMessageSending().then(function (data) {
                t.status = data;
                t.statusDescription = t.printStatus(data);
            });
        }
    }
})();
