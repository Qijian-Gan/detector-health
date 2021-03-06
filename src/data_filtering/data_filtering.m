classdef data_filtering 
    properties
        
        folderLocationFiltering             % Folder that stores the processed data files
        
        input_data                          % Input data
        measures                            % Measures
        numCases                            % Number of cases needed to be updated
        
        interval                            % Interval
        threshold                           % Threshold for identifying break points
        imputation                          % Method to fill in missing data
        smoothing                           % Method to smooth the raw data
        
        
    end
    
    methods ( Access = public )
        
        function [this]=data_filtering(folderLocationFiltering,params,data, measures)
            % This function is to do the data filtering
            
            % Obtain inputs
            this.folderLocationFiltering=folderLocationFiltering;
            this.interval= params.interval;
            this.threshold=params.threshold;
            this.imputation=params.imputation;
            this.smoothing= params.smoothing;
            
            this.input_data=data; % Input data            
            this.measures= measures;    % Measures of detector health         
            this.numCases=size(measures,2); % Number of cases
            
            % Get the number of detector IDs
            detectorIDAll=unique([this.measures.DetectorID]);
            detectorIDNum=length(detectorIDAll);
            
            for i=1:detectorIDNum % Loop for each detector
                detectorID=detectorIDAll(i); % Get the ID
                
                % Get the file name and load the processed data
                fileName=fullfile(this.folderLocationFiltering,sprintf('Processed_data_%d.mat',detectorID));
                if(exist(fileName,'file'))
                    load(fileName); % Variable: process_data, which is a structure 
                else
                    processed_data=[];
                end

                tmp_data=[];                
                for j=1:this.numCases % Loop for the number of cases in the health measures
                    if(detectorID==this.measures(j).DetectorID) % Check the case with the same detector ID
                        dateNum=this.measures(j).DateNum;
                        
                        if(isempty(processed_data) || ...
                                ~any(ismember([[processed_data.id]',[processed_data.day]'],[detectorID,dateNum],'rows')))
                            % Is empty or do not have the corresponding data
                            
                            data_out=this.data_imputation(this.measures(j)); % Do data imputation
                            tmp_data=[tmp_data;struct(...
                                'id', detectorID,...
                                'day', dateNum,...
                                'data',DetectorDataProfile(data_out(:,1),data_out(:,2),data_out(:,3),...
                                data_out(:,4), data_out(:,5), data_out(:,6)))];
                        end                        
                    end
                end
                
                % For each detector, update the processed data and save it
                processed_data=[processed_data;tmp_data];                
                save(fileName,'processed_data');
                clear processed_data
            end
        end
         
        function [data_out]=data_imputation(this,measures)
            % This function is used to do the data imputation and smoothing
            
            % Get all parameters
            detectorID=measures.DetectorID;
            year=measures.Year;
            month=measures.Month;
            day=measures.Day;
            MR=measures.MissingRate;
            
            % Currently not special treatments on the following metrics
            % Try to smooth the data instead
            IR=measures.InconsistencyRate;  
            BP=measures.BreakPoints;        
            
            % Select data
            data_in=this.input_data(ismember(this.input_data(:,1:4), [detectorID,year,month,day],'rows'),:);
            
            % Build time index
            timeIndex=(0:this.interval:24*3600);
            timeIndex=timeIndex(1:end-1);
            
            % Create tmp data file
            tmp_data=zeros(length(timeIndex),6);
            
            % Check whether there are missing values
            if(MR>0)   % If yes, do data imputation
                % First fill in nan values
                curStep=1;
                for i=1:length(timeIndex)
                    if(curStep>size(data_in,1)) % If there is missing data in the end
                        tmp_data(i,:)=[timeIndex(i), zeros(1,5)];
                    else
                        if(floor(data_in(curStep,5)/this.interval)*this.interval==timeIndex(i))
                            % Need to use this function since some data points don't have the same time intervals
                            tmp_data(i,:)=[timeIndex(i),data_in(curStep,6:10)];
                            curStep=curStep+1;
                        else % Missing data in between
                            tmp_data(i,:)=[timeIndex(i), nan(1,5)];
                        end
                    end
                end
                
                % Second call the imputation function
                tmp_data=data_filtering.fill_in_missing_value(tmp_data,this.imputation);                
            else % No need to do imputation
                tmp_data=data_in(:,5:10);
            end
            
            if(any(isnan(tmp_data)))
                disp('has nan value!')
            end
            % Second smooth the data
            tmp_data=data_filtering.smoothing_data(tmp_data,this.smoothing);
            
            if(any(isnan(tmp_data)))
                disp('has nan value!')
            end
            
            data_out=tmp_data;
            
        end
        
        
    end
   
    methods(Static)
        
        function [data_out]=fill_in_missing_value(data_in,imputation_setting)
            % This function is to do data imputation
            
            k= imputation_setting.k; % Set the span
            medianValue = imputation_setting.medianValue;
            
            data_out=data_in;
            for i=1:size(data_out,1) % Loop for all rows
                for j=2:size(data_out,2) % For columns from 2 to end
                    if(isnan(data_out(i,j))) % Find a NaN value
                        if(i==0) % The first data point
                            data_out(i,j)=0;
                            
                        elseif(i>0 && i<=k) % Less than the span
%                             if(medianValue)
%                                 data_out(i,j)=median(data_in(1:i-1,j),'omitnan');
%                             else
%                                 data_out(i,j)=mean(data_in(1:i-1,j),'omitnan');
%                             end
                            
                            if(medianValue)
                                data_out(i,j)=median(data_out(1:i-1,j));
                            else
                                data_out(i,j)=mean(data_out(1:i-1,j));
                            end
                            
                        else % Longer than the span
%                             if(medianValue)
%                                 data_out(i,j)=median(data_in(i-k:i-1,j),'omitnan');
%                             else
%                                 data_out(i,j)=mean(data_in(i-k:i-1,j),'omitnan');
%                             end
                            
                            if(medianValue)
                                data_out(i,j)=median(data_out(i-k:i-1,j));
                            else
                                data_out(i,j)=mean(data_out(i-k:i-1,j));
                            end
                            
                        end
                    end
                end
            end
        end
        
        function [data_out]=smoothing_data(data_in,smoothing_setting)
            % This function is to smooth the data to reduce the noise
            % impact
            
            span=smoothing_setting.span;
            method=smoothing_setting.method;
            degree=smoothing_setting.degree;
            
            data_out=data_in;
            if(strcmp(method,'sgolay')&& ~isnan(degree)) % Use the sgolay method in matlab
                for i=2:size(data_in,2) % For each column
                    data_out(:,i)=smooth(data_in(:,i),span,'sgolay',degree);
                end                    
            else % Use other methods
                for i=2:size(data_in,2) % For each column
                    data_out(:,i)=smooth(data_in(:,i),span,method);
                end 
            end
            
        end
    end
end

