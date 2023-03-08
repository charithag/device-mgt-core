/*
 * Copyright (C) 2018 - 2023 Entgra (Pvt) Ltd, Inc - All Rights Reserved.
 *
 * Unauthorised copying/redistribution of this file, via any medium is strictly prohibited.
 *
 * Licensed under the Entgra Commercial License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       https://entgra.io/licenses/entgra-commercial/1.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.device.mgt.subtype.mgt.exception;

public class SubTypeMgtDAOException extends Exception {

    private static final long serialVersionUID = 3031989080569168761L;
    private String errorMessage;

    public SubTypeMgtDAOException() {
        super();
    }

    public SubTypeMgtDAOException(Throwable cause) {
        super(cause);
    }

    public SubTypeMgtDAOException(String msg, Exception nestedEx) {
        super(msg, nestedEx);
        setErrorMessage(msg);
    }

    public SubTypeMgtDAOException(String message, Throwable cause) {
        super(message, cause);
        setErrorMessage(message);
    }

    public SubTypeMgtDAOException(String msg) {
        super(msg);
        setErrorMessage(msg);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
