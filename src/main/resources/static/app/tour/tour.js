(function () {
    'use strict';
    angular.module('tour', ['ngTouch']).controller('TourController',
            TourController);

    function TourController($log, $routeParams, $location, api, dictionary) {
        var t = this;

        t.statuses = dictionary.getTourStatuses();
        t.taskStatuses = dictionary.getTaskStatuses();
        t.tourStatusMap = new Map(t.statuses.map((i) => [i.id, i.name]));
        t.taskStatusMap = new Map(t.taskStatuses.map((i) => [i.id, i.name]));

        t.isTour = function () {
            return t.task.type == 'TOUR';
        };

        t.isStatusVisible = function (tt) {
            return tt != null && tt.type != 'TOURNAMENT_GROUP';
        };

        t.refresh = function () {
            t.changed = false;
            t.statusChanged = false;
            t.error = '';

            api.getTour($routeParams.id).then(function (data) {
                t.tour = data;
                t.newStatus = t.tour.status;
            });
        };

        t.refresh();

        t.changeStatus = function () {
            api.changeTourStatus($routeParams.id, t.newStatus).then(
                    function (data) {
                        t.refresh();
                    }, function (result) {
                        t.error = result.data.errorMessage;
                    });

        };
    }

})();
