(function () {
    'use strict';
    angular.module('management', ['ngTouch'])
            .controller('ManagementController', ManagementController);
    function ManagementController($log,
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
