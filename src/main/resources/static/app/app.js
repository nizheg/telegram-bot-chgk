(function () {
    'use strict';
    angular.module('chgk', [
        'ngRoute',
        'task',
        'tasks',
        'tour',
        'tours',
        'message',
        'tasks_archive'
    ]).config(config);

    function config($routeProvider) {
        $routeProvider
                .when('/task/:id', {
                    templateUrl: 'app/task/task.html',
                    controller: 'TaskController',
                    controllerAs: 't'

                })
                .when('/task', {
                    templateUrl: 'app/task/task.html',
                    controller: 'TaskController',
                    controllerAs: 't'

                })
                .when('/tasks', {
                    templateUrl: 'app/task/tasks.html',
                    controller: 'TasksController',
                    controllerAs: 't'

                })
                .when('/tasks/import', {
                    templateUrl: 'app/task/import.html'
                })
                .when('/tour/:id', {
                    templateUrl: 'app/tour/tour.html',
                    controller: 'TourController',
                    controllerAs: 't'

                })
                .when('/tours', {
                    redirectTo: "/tour/0"

                })
                .when('/tours/new', {
                    templateUrl: 'app/tour/tours.html',
                    controller: 'ToursController',
                    controllerAs: 't'

                })
                .when('/message', {
                    templateUrl: 'app/admin/message.html',
                    controller: 'MessageController',
                    controllerAs: 't'

                })
                .when('/tasks_archive', {
                    templateUrl: 'app/admin/tasks-archive.html',
                    controller: 'TasksArchiveController',
                    controllerAs: 't'
                })
                .otherwise({
                    templateUrl: 'app/main.html'
                });
    }
})();
