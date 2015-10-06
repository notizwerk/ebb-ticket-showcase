angular.module('app.services',[])
    
    .constant('BACKEND','localhost:8090')

    .service('backend', function($rootScope, $http, BACKEND) {
   
        return {
            login: function (userName, success, failure) {
                $http({
                    url:'http://'+BACKEND+'/api/session',
                    method:'post',
                    data:{userName: userName },
                    withCredentials:true,
                }).then(function(response) {
                    console.log("login response",response);
                    success(response.data.sessionId);
                }, function(response) {
                    console.log("login failed",response);
                    failure("http error");
                });
            }
        };
    
    }).service('ping', function($http, BACKEND) {
        return {
            ping: function (success,failure) {
                $http({
                    url:'http://' +BACKEND +'/api/ping',
                    method:'post',
                    data:{ping: "pong"},
                    withCredentials:true,
                }).then(function(response) {
                    console.log("ping response",response);
                    success(response.data);
                }, function(response) {
                    console.log("ping failure",response);
                    failure(response.data);
                });
            }
        };
    
    }).service('eventBusService', function (BACKEND) {
    
        this.eb = undefined;
        this.start = function (authorization,onOpen,onClose,onError) {
            this.eb = new EventBus("http://"+BACKEND+"/eventbus");
            this.eb.defaultHeaders = {'authorization':authorization};
            this.eb.onopen = onOpen;
            this.eb.onclose = function () {
                console.log("eventbus closed");
                onClose();
            };
            this.eb.onerror = function (error) {
                console.log("eventbus error:", error);
                onError();
            };
            return this.eb;
        }
        return this;
    });
