(function() {
    var module=angular.module("es.logongas.ix3.validator",[]);

    var mensajePatterns = {
        required: "No puede estar vacio",
        email: "No tiene el formato de EMail",
        maxlength: "Debe tener un tamaño menor o igual a {{maxlength}}",
        minlength: "Debe tener un tamaño mayor o igual a {{minlength}}",
        pattern:"No cumple la expresión regular: {{'pattern'}}",
        min:"Debe ser un valor mayor o igual a {{min}}",
        max:"Debe ser un valor menor o igual a {{max}}",
        url:"No tiene el formato de una URL",
        integer:"El valor '{{value}}' no es un número"
    };




    function Validator($interpolate) {
        function getNormalizeAttributeName(attributeName) {
            var normalizeAttributeName;
            var separator;

            if (attributeName.indexOf("-")>=0) {
                separator="-";
            } else if (attributeName.indexOf(":")>=0) {
                separator=":";
            } else {
                separator=undefined;
            }

            var parts=attributeName.split(separator);
            normalizeAttributeName=parts[parts.length-1];

            return normalizeAttributeName;
        }

        function getMessage(inputElement,errorType) {
            var messagePattern = mensajePatterns[errorType];
            if (typeof (messagePattern) === "undefined") {
                messagePattern = errorType;
            }

            var messageEvaluator=$interpolate(messagePattern);

            var attributes={
                value:inputElement.val()
            };
            for(var attIndex in inputElement.attributes) {
                var attName=inputElement.attributes[attIndex].nodeName;
                if (attName!==undefined) {
                    var value=inputElement.attributes[attIndex].nodeValue;
                    attributes[getNormalizeAttributeName(attName)]=value;
                }
            }

            var message=messageEvaluator(attributes);
            return message;
        }

        /**
         * Dado el nombre de un "input" obtiene el label asociado
         */
        function getLabel(inputElement,defaultLabel) {
            var label;

            if (inputElement.attr('id')) {
                var labelElement = $('label[for="'+ inputElement.attr('id') +'"]');
                if (labelElement.length > 0 ) {
                    label=labelElement[0].text();
                } else {
                    label=defaultLabel;
                }
            } else {
                label=defaultLabel;
            }

            return label;
        }


        this.validate=function(form) {
            var businessMessages = [];

            var formElement=$("form[name='" + form.$name + "']");

            for (var propertyName in form) {
                if (typeof(propertyName)==="string" && propertyName.charAt(0) != "$") {
                    if (form[propertyName].$error) {
                        var inputElement=$("[name='" + propertyName + "']",formElement);
                        for (var errorType in form[propertyName].$error) {
                            if (form[propertyName].$error[errorType] === true) {
                                businessMessages.push({
                                    propertyName: propertyName,
                                    label: getLabel(inputElement,propertyName),
                                    message: getMessage(inputElement,errorType)
                                });
                            }

                        }
                    }
                }
            }

            return businessMessages;
        }



    }

    module.service("validator",Validator);

})();