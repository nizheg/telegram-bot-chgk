(function () {
    'use strict';
    angular.module('tasks', ['ngTouch'])
            .controller('TasksController', TasksController);
    function TasksController($log,
                             $routeParams,
                             $location,
                             api) {
        var t = this;
        t.refresh = function () {
            api.getTasks().then(function (data) {
                t.tasks = data;
            });
        };
        t.refresh();
    }
})();
