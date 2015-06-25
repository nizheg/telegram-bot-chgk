(function () {
    'use strict';
    angular.module('chgk')
            .factory('dictionary', dictionary);
    function dictionary() {
        return {
            getTaskStatuses: function () {
                return [{id: 'NEW', name: 'Новый'}, {id: 'PUBLISH_READY', name: 'Готов к публикации'}, {id: 'PUBLISHED', name: 'Опубликован'}, {
                    id: 'DELETED',
                    name: 'Удален'
                }]
            },
            getAnswerTypes: function () {
                return [{id: 'EXACT', name: 'Точно'}, {id: 'APPROXIMATE', name: 'Примерно'}, {id: 'CONTAINS', name: 'Содержит'}, {
                    id: 'EXACT_WITH_PUNCTUATION',
                    name: 'Абсолютно'
                }]
            },
            getTourStatuses: function () {
                return [{id: 'NEW', name: 'Новый'}, {id: 'PUBLISHED', name: 'Опубликован'}, {id: 'DELETED', name: 'Удален'}]
            }
        };
    }

})();
