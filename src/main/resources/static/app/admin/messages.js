(function () {
    'use strict';
    angular.module('messages', ['ngTouch'])
            .controller('MessagesController', MessagesController);
    function MessagesController($log,
                               $routeParams,
                               $location,
                               api,
                               dictionary) {
      var t = this;
      t.page = $routeParams.page;
      t.messages = [];
      if (t.page === undefined) {
           t.page=1;
        }

      api.getMessageStatuses(t.page).then(function(data) {
        t.messages = data;
      });

      t.previousPage = function() {
        var previousPage = t.page > 1 ? t.page - 1 : 1;
        $location.search('page', previousPage);
      };

      t.nextPage = function() {
        $location.search('page', (t.page + 1));
      };

      t.getStatusText = function(sendingMessageStatus) {
            var description = ' ';
            if (sendingMessageStatus.sendMessageData != null) {
                description += sendingMessageStatus.sendMessageData.text;
            } else {
                description += sendingMessageStatus.forwardMessageData.text;
            }
            return description;
      };

      t.getStatusStatuses = function(sendingMessageStatus) {
        var description = '';
            Object.keys(sendingMessageStatus.statuses).forEach(function(key,index) {
                      description += ' '+ t.getStatusDescription(key) + ':' + sendingMessageStatus.statuses[key];
                  });
                  return description;
      }

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


     }

})();
