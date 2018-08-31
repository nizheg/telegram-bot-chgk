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
        t.archiving = false;
        t.archiveTasks = function () {
            t.archiving = true;
            t.archived = null;
            api.archiveTasks().then(function (data) {
                t.archiving = false;
                t.archived = data;
            });
        }
    }
})();
