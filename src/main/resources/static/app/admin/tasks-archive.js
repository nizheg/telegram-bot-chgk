(function () {
    'use strict';
    angular.module('tasks_archive', ['ngTouch'])
            .controller('TasksArchiveController', TasksArchiveController);
    function TasksArchiveController($log,
                               $routeParams,
                               $location,
                               api,
                               dictionary) {
        var t = this;
        t.archived = null;
        t.archiveTasks = function () {
            t.archived = '-';
            api.archiveTasks().then(function (data) {
                t.archived = data;
            });
        }
    }
})();
