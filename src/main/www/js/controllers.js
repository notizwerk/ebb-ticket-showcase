
angular.module('app.controllers', [])

    .controller('MainCtrl', function ($scope, backend, ping, eventBusService) {
        $scope.userName="";
        $scope.sessionId = "";
        $scope.ping =function () {
            ping.ping();
        };
        
        $scope.login = function () {
            backend.login($scope.userName, function(sessionId) {
                $scope.sessionId = sessionId;
            }, function () {
                $scope.sessionId = "";
            });
        };

        $scope.ping = function () {
            ping.ping(function(data) {
               $scope.pingResponse = data;
            }, function () {
                $scope.pingResponse = "<FAILED>";
            });
        };

        $scope.eventbusStatus = "closed";
        $scope.startEb = function () {
            $scope.eventbus = eventBusService.start( $scope.sessionId, function () {
                $scope.$apply(function() {$scope.eventbusStatus = "started";});
            },function() {
                $scope.$apply(function() {$scope.eventbusStatus = "closed";});
            },function() {
                $scope.$apply(function() {$scope.eventbusStatus = "failed";});
            });
        };
    
        $scope.replyMessage = "";
        $scope.message = "";
        $scope.sendMessage = function() {
          $scope.eventbus.send("ping", { action: "send me a pong", text:$scope.message } , function(err,msg) {
            console.log("received reply message:", msg);
              $scope.$apply(function() {
                  $scope.replyMessage = JSON.stringify(msg);
              });
          });
        };
        
        $scope.registerHandler = function() {
            $scope.eventbus.registerHandler("topic", function (err,msg) {
                console.log("received a message:", msg);
                $scope.$apply(function() {
                    $scope.receivedMessage =JSON.stringify(msg);
                });
            });
        };
    });
