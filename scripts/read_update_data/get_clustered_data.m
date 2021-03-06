%% This script is to run the detector health report for the City of Arcadia
clear
clc
close all

%% Load the detector config file
config=load_config('Arcadia_detector_config.xlsx');
config.detectorConfig=config.detector_property('Detector_Properties');


%% Extract the clustered data for given settings

day={'All','Sunday','Monday','Tuesday','Wednesday','Thursday','Friday','Saturday','Weekday','Weekend'};
ptr=sensor_count_provider; % Create the object with default file locations

year=nan;
month=nan;
day=nan;
for d=1:10
    
    queryMeasures=struct(...
        'year',     year,...
        'month',    month,...
        'day',      day,...
        'dayOfWeek',d-1,...
        'median', 1,...
        'timeOfDay', 0);

    numDetector=size(config.detectorConfig,1);
    
    clustered_data=[];
    for i=1:numDetector % Loop for all detectors
        % Get the detector ID: Intersection ID + Sensor ID
        if(config.detectorConfig(i).SensorID<10) % Sensor ID <10
            detectorID={sprintf('%d0%d',config.detectorConfig(i).IntersectionID,config.detectorConfig(i).SensorID)};
        else
            detectorID={sprintf('%d%d',config.detectorConfig(i).IntersectionID,config.detectorConfig(i).SensorID)};
        end
        
        % Get the clustered data with given query measures
        tmp_data=ptr.clustering(detectorID,queryMeasures);
        
        % Store all clustered data into the same file
        clustered_data=[clustered_data;struct(...
            'detectorID',detectorID,...
            'config',config.detectorConfig(i),...
            'data',tmp_data.data,...
            'status',tmp_data.status)];
    end
    
    save(fullfile(ptr.outputFolderLocation,sprintf('Clustered_data_%s.mat',day{d})),'clustered_data')
    
end
