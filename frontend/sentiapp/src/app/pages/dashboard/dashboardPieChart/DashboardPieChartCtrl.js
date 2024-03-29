/**
 * @author v.lugovksy
 * created on 16.12.2015
 */
(function () {
    'use strict';

    angular.module('BlurAdmin.pages.dashboard')
        .controller('DashboardPieChartCtrl', DashboardPieChartCtrl);

    /** @ngInject */
    function DashboardPieChartCtrl($scope, $element, $window, $timeout, $auth) {
        $scope.charts = [{
            color: 'rgba(255,255,255,0.4)',
            description: 'Fast Search',
            link: 'main.fastsearch',
            icon: 'ion-android-search'
        }, {
            color: 'rgba(255,255,255,0.4)',
            description: 'Geo Search',
            link: 'main.geosearch',
            icon: 'ion-ios-location-outline'
        }];

        if ($auth.isAuthenticated()) {
            $scope.charts.push({
                color: 'rgba(255,255,255,0.4)',
                description: 'New research',
                link: 'main.research',
                icon: 'ion-clock'
            });
        }


        function getRandomArbitrary(min, max) {
            return Math.random() * (max - min) + min;
        }

        function loadPieCharts() {
            $('.chart').each(function () {
                var chart = $(this);
                chart.easyPieChart({
                    easing: 'easeOutBounce',
                    onStep: function (from, to, percent) {
                        $(this.el).find('.percent').text(Math.round(percent));
                    },
                    barColor: chart.attr('rel'),
                    trackColor: 'rgba(0,0,0,0)',
                    size: 84,
                    scaleLength: 0,
                    animation: 2000,
                    lineWidth: 9,
                    lineCap: 'round',
                });
            });

            $('.refresh-data').on('click', function () {
                updatePieCharts();
            });
        }

        function updatePieCharts() {
            $('.pie-charts .chart').each(function (index, chart) {
                $(chart).data('easyPieChart').update(getRandomArbitrary(55, 90));
            });
        }

        $timeout(function () {
            loadPieCharts();
            updatePieCharts();
        }, 1000);
    }
})();