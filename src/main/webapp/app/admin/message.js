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
                case 'INIT':
                    t.message = status.sendingMessage;
                    return 'Сообщение для Forward инициировано';
                case 'REJECTED':
                    return 'Ошибка. ' + status.errorMessage;
                case 'IN_PROCESS':
                    return 'Отправка в процессе.\nВыполнено: ' + status.finished + ' из ' + status.totalCount +
                            '\nОтправляемое сообщение: \n' + status.sendingMessage;
                case 'FINISHED':
                    return 'Отправка завершена. Отправлено ' + status.finished + ' из ' + status.totalCount +
                            '\nОтправленное сообщение: \n' + status.sendingMessage;
                case 'CANCELLED':
                    return 'Отправка отменена. Отправлено ' + status.finished + ' из ' + status.totalCount +
                            '\nОтправляемое сообщение: \n' + status.sendingMessage;
                default :
                    return '';
            }

        };

        t.isInProcess = function () {
            return 'IN_PROCESS' == t.status.status || 'INIT' == t.status.status;
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
