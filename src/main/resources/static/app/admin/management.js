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

        t.workStatus = {
            "periodInSeconds" : "1",
            "batchSize" : "1",
            "started" : false
        };
        t.getWorkingStatus = function() {
            api.isWorkerStarted().then(function(data) {
                t.workStatus = data;
            });
        }

        t.startWorking = function() {
            api.startWorker(t.workStatus).then(function(data) { t.getWorkingStatus(); });
        }
        t.stopWorking = function() {
            api.stopWorker().then(function(data) { t.getWorkingStatus(); })
        }
        t.getWorkingStatus();
    }
})();
