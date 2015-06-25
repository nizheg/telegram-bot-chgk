(function () {
    'use strict';
    angular.module('task', ['ngTouch'])
            .controller('TaskController', TaskController);

    function TaskController($log,
                            $routeParams,
                            $location,
                            api,
                            dictionary) {
        var t = this;

        t.statuses = dictionary.getTaskStatuses();
        t.answerTypes = dictionary.getAnswerTypes();

        t.isNew = function () {
            return $routeParams.id == null;
        };

        t.refresh = function () {
            t.changed = false;
            t.statusChanged = false;
            t.changedPictures = {};
            t.changedCommentPictures = {};
            if (t.isNew()) {
                t.task = {};
                t.tour = {};
                t.tourName = '';
                t.answers = [];
                t.categories = [];
                t.newStatus = 'NEW';
                t.pictures = [];
                t.commentPictures = [];
            } else {
                api.getTask($routeParams.id).then(function (data) {
                    t.task = data;
                    t.newStatus = t.task.status;
                    if (t.task.tourId != null) {
                        api.getTour(t.task.tourId).then(function (data) {
                            t.tour = data;
                        });
                    }
                });
                api.getAnswers($routeParams.id).then(function (data) {
                    t.answers = data;
                });
                api.getTaskPictures($routeParams.id).then(function (data) {
                    t.pictures = data;
                });
                api.getCommentPictures($routeParams.id).then(function (data) {
                    t.commentPictures = data;
                });
                api.getCategories($routeParams.id).then(function (data) {
                    t.categories = data;
                    return data;
                }).then(function (data1) {
                    api.getAllCategories().then(function (data) {
                        var map = {};
                        for (var i = 0; i < data1.length; i++) {
                            map[data1[i].id] = data1[i].name;
                        }
                        t.allCategories = [];
                        for (var i = 0; i < data.length; i++) {
                            if (map[data[i].id] == null) {
                                t.allCategories.push(data[i]);
                            }
                        }
                    });
                });
            }
        };

        t.refresh();

        t.addAnswer = function () {
            t.answers.push({
                text: '',
                taskId: $routeParams.id,
                type: 'APPROXIMATE'
            });
        };

        t.addCategory = function () {
            if (t.newCat != null) {
                api.addCategory($routeParams.id, t.newCat.id)
                        .then(function (data) {
                            t.refresh();
                        });
            }
        };

        t.save = function () {
            api.saveTask(t.task).then(function (savedTask) {
                for (var i = 0; i < t.answers.length; i++) {
                    t.answers[i].taskId = savedTask.id;
                    api.saveAnswer(t.answers[i]).then(function (data) {
                        t.refresh();
                    });
                }
                $location.path('/task/' + savedTask.id);
            });
        };

        t.deleteAnswer = function (index, answer) {
            t.answers.splice(index, 1);
            if (answer.id != null) {
                api.deleteAnswer(answer.id).then(function (data) {
                    t.refresh();
                });
            }
        };

        t.changeStatus = function () {
            if (t.isNew()) {
                return;
            }
            api.changeStatus($routeParams.id, t.newStatus).then(function (data) {
                t.refresh();
            });
        };

        t.deleteCategory = function (category) {
            api.deleteCategory($routeParams.id, category.id).then(function (data) {
                t.refresh();
            });
        };

        t.nextTask = function () {
            $location.path('/task/' + (parseInt($routeParams.id, 10) + 1));
        };

        t.previousTask = function () {
            $location.path('/task/' + (parseInt($routeParams.id, 10) - 1));
        };

        t.sendTaskMessageToMe = function () {
            api.sendTaskMessageToMe($routeParams.id)
        };

        t.addPicture = function () {
            t.pictures.push({
                position: 0
            });
        };

        t.changedPicture = function (index) {
            t.changedPictures[index] = true;
        }

        t.isPictureChanged = function (index) {
            return t.changedPictures[index];
        }

        t.deletePicture = function (index, picture) {
            t.pictures.splice(index, 1);
            t.changedPictures[index] = false;
            if (picture.id != null) {
                api.deletePictureFromTask($routeParams.id, picture.id).then(function (data) {
                    t.refresh();
                });
            }
        };

        t.savePicture = function (picture) {
            api.savePictureToTask($routeParams.id, picture).then(function (data) {
                t.refresh()
            });
        };

        t.addCommentPicture = function () {
            t.commentPictures.push({
                position: 0
            });
        };

        t.changedCommentPicture = function (index) {
            t.changedCommentPictures[index] = true;
        }

        t.isCommentPictureChanged = function (index) {
            return t.changedCommentPictures[index];
        }

        t.deleteCommentPicture = function (index, picture) {
            t.commentPictures.splice(index, 1);
            t.changedCommentPictures[index] = false;
            if (picture.id != null) {
                api.deletePictureFromComment($routeParams.id, picture.id).then(function (data) {
                    t.refresh();
                });
            }
        };

        t.saveCommentPicture = function (picture) {
            api.savePictureToComment($routeParams.id, picture).then(function (data) {
                t.refresh()
            });
        };
    }

})();
