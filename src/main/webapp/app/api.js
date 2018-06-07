(function () {
    'use strict';
    angular.module('chgk')
            .factory('api', api);
    function api($http) {
        return {
            getTasks: function () {
                return $http.get('api/task?status=NEW')
                        .then(function (response) {
                            return response.data;
                        })
            },
            getTask: function (taskId) {
                return $http.get('api/task/' + taskId)
                        .then(function (response) {
                            return response.data;
                        })
            },
            getAnswers: function (taskId) {
                return $http.get('api/answer?taskId=' + taskId)
                        .then(function (response) {
                            return response.data;
                        })
            },
            getCategories: function (taskId) {
                return $http.get('api/task/' + taskId + '/category')
                        .then(function (response) {
                            return response.data;
                        })
            },
            getAllCategories: function () {
                return $http.get('api/category')
                        .then(function (response) {
                            return response.data;
                        })
            },
            addCategory: function (taskId, categoryId) {
                return $http.post("api/task/" + taskId + "/category?categoryId=" + categoryId)
                        .then(function (response) {
                            return response.data
                        });
            },
            deleteCategory: function (taskId, categoryId) {
                return $http.delete("api/task/" + taskId + "/category/" + categoryId)
                        .then(function (response) {
                            return response.data
                        });
            },
            saveAnswer: function (answer) {
                if (answer.id == null) {
                    return $http.post("api/answer", answer).then(function (response) {
                        return response.data
                    });
                } else {
                    return $http.put("api/answer/" + answer.id, answer).then(function (response) {
                        return response.data
                    });
                }
            },
            deleteAnswer: function (id) {
                return $http.delete("api/answer/" + id)
                        .then(function (response) {
                            return response.data
                        });
            },
            saveTask: function (task) {
                if (task.id == null) {
                    return $http.post("api/task", task).then(function (response) {
                        return response.data
                    });
                } else {
                    return $http.put("api/task/" + task.id, task).then(function (response) {
                        return response.data
                    });
                }
            },
            getTaskPictures: function (taskId) {
                return $http.get("api/task/" + taskId + "/taskPicture").then(function (response) {
                    return response.data
                });
            },
            savePictureToTask: function (taskId, picture) {
                if (picture.id == null) {
                    return $http.post("api/picture", {"sourceUrl": picture.sourceUrl, "caption": picture.caption}).then(function (response) {
                        var savedPicture = response.data;
                        $http.post("api/task/" + taskId + "/taskPicture",
                                {"id": savedPicture.id, "position": picture.position}).then(function (response) {
                                    return true;
                                })
                    });
                } else {
                    return $http.put("api/task/" + taskId + "/taskPicture/" + picture.id, picture).then(function (response) {
                        return true;
                    });
                }
            },
            deletePictureFromTask: function (taskId, pictureId) {
                return $http.delete("api/task/" + taskId + "/taskPicture/" + pictureId).then(function (response) {
                    return true;
                });
            },
            getCommentPictures: function (taskId) {
                return $http.get("api/task/" + taskId + "/commentPicture").then(function (response) {
                    return response.data
                });
            },
            savePictureToComment: function (taskId, picture) {
                if (picture.id == null) {
                    return $http.post("api/picture", {"sourceUrl": picture.sourceUrl, "caption": picture.caption}).then(function (response) {
                        var savedPicture = response.data;
                        $http.post("api/task/" + taskId + "/commentPicture",
                                {"id": savedPicture.id, "position": picture.position}).then(function (response) {
                                    return true;
                                })
                    });
                } else {
                    return $http.put("api/task/" + taskId + "/commentPicture/" + picture.id, picture).then(function (response) {
                        return true;
                    });
                }
            },
            deletePictureFromComment: function (taskId, pictureId) {
                return $http.delete("api/task/" + taskId + "/commentPicture/" + pictureId).then(function (response) {
                    return true;
                });
            },
            changeStatus: function (taskId, status) {
                return $http.patch("api/task/" + taskId, {
                    "status": status
                }).then(function (response) {
                    return response.data
                });
            },
            sendMessage: function (text, disableWebPagePreview, parseMode) {
                return $http.post('api/message', {"receiver": "all", "text": text, "disableWebPagePreview":
                disableWebPagePreview, "parseMode": parseMode})
                .then(function (response) {
                    return response.data
                });
            },
            sendMessageToMe: function (text, disableWebPagePreview, parseMode) {
                return $http.post('api/message', {"receiver": "me", "text": text, "disableWebPagePreview":
                disableWebPagePreview, "parseMode": parseMode})
                .then(function (response) {
                    return response.data
                });
            },
            getMessageStatus: function () {
                return $http.get('api/message/status')
                        .then(function (response) {
                            return response.data;
                        })
            },
            cancelMessageSending: function () {
                return $http.post('api/message/status', {"status": 'CANCELLED'}).then(function (response) {
                    return response.data
                });
            },
            sendTaskMessageToMe: function (taskId) {
                return $http.post("api/message", {"receiver": "me", "taskId": taskId}).then(function (response) {
                    return response.data
                });
            },
            getTour: function (tourId) {
                return $http.get("api/tour/" + tourId).then(function (response) {
                    return response.data;
                });
            },
            getNewTournaments: function () {
                return $http.get('api/tour?type=TOURNAMENT&status=NEW')
                        .then(function (response) {
                            return response.data;
                        })
            },
            changeTourStatus: function (tourId, status) {
                return $http.patch("api/tour/" + tourId, {
                    "status": status
                }).then(function (response) {
                    return response.data
                });
            }
        };
    }

})();
