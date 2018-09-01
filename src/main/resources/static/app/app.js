(function () {
    'use strict';
    angular.module('chgk', [
        'ngRoute',
        'task',
        'tasks',
        'tour',
        'tours',
        'message',
        'messages',
        'management'
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
                .when('/messages', {
                    templateUrl: 'app/admin/messages.html',
                    controller: 'MessagesController',
                    controllerAs: 't'

                })
                .when('/message/:id', {
                    templateUrl: 'app/admin/message.html',
                    controller: 'MessageController',
                    controllerAs: 't'

                })
                .when('/message', {
                    templateUrl: 'app/admin/message.html',
                    controller: 'MessageController',
                    controllerAs: 't'

                })
                .when('/management', {
                    templateUrl: 'app/admin/management.html',
                    controller: 'ManagementController',
                    controllerAs: 't'
                })
                .otherwise({
                    templateUrl: 'app/main.html'
                });
    }
})();
