(function () {
    'use strict';
    angular.module('tours', ['ngTouch'])
            .controller('ToursController', ToursController);
    function ToursController($log,
                             $routeParams,
                             $location,
                             api) {
        var t = this;
        api.getNewTournaments().then(function (data) {
            t.tours = data;
        });
    }
})();
